#!/usr/bin/python

import sys;

def sortedKeys(dict):
	keys = dict.keys()
	keys.sort()
	return keys

if __name__ == '__main__':
	
	i = 0

	width = 1200
	height = 600

	reader = open(sys.argv[1], "r")
	jsname = '%s.js' % sys.argv[1]
	jswriter = open(jsname, "w")
	htmlwriter = open(sys.argv[1] + '.html', "w")

	charts = []	

	colors = ["'#F00'", "'#080'", "'#00009c'", "'orange'", "'purple'", "'grey'", "'cyan'", "'black'"]


	name = ''
	threads = ''
	messages = ''

	loggerMap = {}	
	threadsMap = {}
	messagesMap = {}
	timeMap = {}
	gcMap = {}

	
	for line in reader:
		#print i , " " , line	
		line = line.strip()
		idx = i % 3
		a = line.split()

		if idx == 0 :
			name = a[0]
			threads = a[4]
			messages = a[6]

			loggerMap[name] = True
			threadsMap[int(threads)] = True
			messagesMap[int(messages)] = True

		elif idx == 1 :
			time = float(a[3])
			key = name + '_' + threads + '_' + messages
			if key not in timeMap:
				timeMap[key] = []
			timeMap[key].append(time)
		elif idx == 2 :	
			if len(line) == 0:
				gc = 0.0
			else :
				gc = float(line)
			
			key = name + '_' + threads + '_' + messages
			if key not in gcMap:
				gcMap[key] = []
			gcMap[key].append(gc)

		i = i + 1
	
	v = []
	v.append('threads')
	v.append('messages')
	for name in sortedKeys(loggerMap):
		v.append(name + " throughput")	
		v.append(name + " gc")	

	jswriter.write('function drawChart(){ \n')

	for threads in sortedKeys(threadsMap):
		v = []
		v.append(str(threads))
		for messages in sortedKeys(messagesMap):
			v2 = list(v)	
			v2.append(str(messages))
	
			for name in sortedKeys(loggerMap):
				key = name + '_' + str(threads) + '_' + str(messages)
				avg = sum(timeMap[key])/len(timeMap[key])
				v2.append('%.2f' % (float(messages) / avg))
				# into ms
				avg = sum(gcMap[key])/len(gcMap[key])
				v2.append('%.2f' % (1000 * avg))

	for threads in sortedKeys(threadsMap):
		t = str(threads)
		ts = str(threads)
		tname = "threads"
		if threads == 1:
			tname = "thread"			
			ts = 'single'
		v = []
		for messages in sortedKeys(messagesMap):
			v2 = []	
			v2.append("'" + str(messages) + "'")
			vc = ['Messages']
	
			for name in sortedKeys(loggerMap):
				key = name + '_' + str(threads) + '_' + str(messages)
				avg = sum(timeMap[key])/len(timeMap[key])
				v2.append('%.2f' % (float(messages) / avg))
				# into ms
				#v2.append('%.2f' % (1000 * gcMap[key]))
				vc.append(name)
			v.append('[' + ( ','.join(v2)) + ']')	
			logger_names = ','.join(vc)

		logger_names = '';
		for i in range(0, len(vc)):
			type = 'number'
			if i == 0:
				type = 'string'	
			logger_names = "%s\n\
		data.addColumn('%s', '%s');" % (logger_names, type, vc[i])

		chartname = 'throughput_%s_chart' % t

		charts.append(chartname)

		jswriter.write("\t/********* %s %s **************/ \n\
		//throughput	\n\
		data = new google.visualization.DataTable();	\n\
			%s \n\
		data.addRows([	\n\
			%s\n\
		]);				\n\
	\n\
        chart = new google.visualization.LineChart(document.getElementById('%s')); \n\
        chart.draw(data, \n\
		{		\n\
          width: %d, height: %d, \n\
          title: 'Throughput, %s %s', \n\
          hAxis: {title: 'number of messages',  titleTextStyle: {color: '#000'}, logScale: true}, \n\
          vAxis: {title: 'messages / ms', gridlines: {color: '#ccc', count: 8}}, \n\
          legend: {position: 'right', textStyle: {color: 'black', fontSize: 10}}, \n\
		  colors: [%s]\n\
        });\n\
		" % (ts, tname, logger_names, ',\n\t\t\t'.join(v), chartname, width, height, ts, tname, ','.join(colors)))
		

		v = []
		for messages in sortedKeys(messagesMap):
			v2 = []
			v2.append("'" + str(messages) + "'")
			vc = ['Messages']
	
			for name in sortedKeys(loggerMap):
				key = name + '_' + str(threads) + '_' + str(messages)
				#v2.append('%.2f' % (float(messages) / timeMap[key]))
				# into ms
				avg = sum(gcMap[key])/len(gcMap[key])
				v2.append('%.2f' % (1000 * avg))
				vc.append(name)
			v.append('[' + ( ','.join(v2)) + ']')	

		logger_names = '';
		for i in range(0, len(vc)):
			type = 'number'
			if i == 0:
				type = 'string'	
			logger_names = "%s\n\
		data.addColumn('%s', '%s');" % (logger_names, type, vc[i])
		

		chartname = 'gc_%s_chart' % t

		charts.append(chartname)


		jswriter.write("//gc \n\
		data = new google.visualization.DataTable(); \n\
		%s\n\
		data.addRows([ \n\
			%s \n\
		]); \n\
\n\
		chart = new google.visualization.LineChart(document.getElementById('%s'));\n\
        chart.draw(data, \n\
        {\n\
          width: %d, height: %d,\n\
          title: 'Total stop the world, %s %s',\n\
          hAxis: {title: 'number of messages',  titleTextStyle: {color: '#000'}, logScale: true},\n\
          vAxis: {title: 'ms', gridlines: {color: '#ccc', count: 8}},\n\
          legend: {position: 'right', textStyle: {color: 'black', fontSize: 10}},\n\
		  colors: [%s]\n\
        });\n" % (logger_names, ',\n\t\t\t'.join(v), chartname, width, height, ts, tname, ','.join(colors)))

	jswriter.write('}\n')	

	htmlwriter.write('<html>\n\
<body>\n\
<div id="chart"></div>\n\
%s\n\
<script type="text/javascript" src="https://www.google.com/jsapi"></script>\n\
<script type="text/javascript" src="%s"></script>\n\
<script type="text/javascript">\n\
      google.load("visualization", "1", {packages:["imagelinechart", "imagechart", "corechart"]});\n\
      google.setOnLoadCallback(drawChart);\n\
</script>\n\
</body> \n\
</html>\n' % ( "\n".join(map(lambda c: '<div id="%s"></div>' % c, charts)), jsname ) )

	jswriter.close()
	htmlwriter.close()
