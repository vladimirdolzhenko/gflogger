#!/usr/bin/python

import sys;

def sortedKeys(dict):
	keys = dict.keys()
	keys.sort()
	return keys

if __name__ == '__main__':
	
	i = 0

	reader = open(sys.argv[1], "r")
	writer = open(sys.argv[1] + '.csv', "w")
	writer2 = open(sys.argv[1] + '.js', "w")

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

		if idx == 1 :
			time = float(a[3])
			key = name + '_' + threads + '_' + messages
			if key not in timeMap:
				timeMap[key] = 0.0	
			timeMap[key] = timeMap[key] + time/3
		if idx == 2 :	
			if len(line) == 0:
				gc = 0.0
			else :
				gc = float(line)
			
			key = name + '_' + threads + '_' + messages
			if key not in gcMap:
				gcMap[key] = 0.0	
			gcMap[key] = gcMap[key] + gc/3

		i = i + 1
	
	v = []
	v.append('threads')
	v.append('messages')
	for name in sortedKeys(loggerMap):
		v.append(name + " throughput")	
		v.append(name + " gc")	
	writer.write('%s\n' % ','.join(v))

	for threads in sortedKeys(threadsMap):
		v = []
		v.append(str(threads))
		for messages in sortedKeys(messagesMap):
			v2 = list(v)	
			v2.append(str(messages))
	
			for name in sortedKeys(loggerMap):
				key = name + '_' + str(threads) + '_' + str(messages)
				v2.append('%.2f' % (float(messages) / timeMap[key]))
				# into ms
				v2.append('%.2f' % (1000 * gcMap[key]))
			writer.write('%s\n' % ','.join(v2))

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
				v2.append('%.2f' % (float(messages) / timeMap[key]))
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


		writer2.write("/********* %s %s **************/ \n\
		//throughput	\n\
		data = new google.visualization.DataTable();	\n\
		%s \n\
		data.addRows([	\n\
%s				\n\
		]);				\n\
	\n\
        chart = new google.visualization.LineChart(document.getElementById('throughput_%s_chart')); \n\
        chart.draw(data, \n\
		{		\n\
          width: 600, height: 250, \n\
          title: 'Throughput, %s %s', \n\
          hAxis: {title: 'number of messages',  titleTextStyle: {color: '#000'}, logScale: true}, \n\
          vAxis: {title: 'messages / ms', gridlines: {color: '#ccc', count: 8}}, \n\
          legend: {position: 'right', textStyle: {color: 'black', fontSize: 10}} \n\
        });\n\
		" % (ts, tname, logger_names, ',\n'.join(v), t, ts, tname))
		

		v = []
		for messages in sortedKeys(messagesMap):
			v2 = []
			v2.append("'" + str(messages) + "'")
			vc = ['Messages']
	
			for name in sortedKeys(loggerMap):
				key = name + '_' + str(threads) + '_' + str(messages)
				#v2.append('%.2f' % (float(messages) / timeMap[key]))
				# into ms
				v2.append('%.2f' % (1000 * gcMap[key]))
				vc.append(name)
			v.append('[' + ( ','.join(v2)) + ']')	

		logger_names = '';
		for i in range(0, len(vc)):
			type = 'number'
			if i == 0:
				type = 'string'	
			logger_names = "%s\n\
		data.addColumn('%s', '%s');" % (logger_names, type, vc[i])
	

		writer2.write("//gc \n\
		data = new google.visualization.DataTable(); \n\
		%s\n\
		data.addRows([ \n\
%s \n\
		]); \n\
\n\
		chart = new google.visualization.LineChart(document.getElementById('gc_%s_chart'));\n\
        chart.draw(data, \n\
        {\n\
          width: 600, height: 250,\n\
          title: 'Total stop the world, %s %s',\n\
          hAxis: {title: 'number of messages',  titleTextStyle: {color: '#000'}, logScale: true},\n\
          vAxis: {title: 'ms', gridlines: {color: '#ccc', count: 8}},\n\
          legend: {position: 'right', textStyle: {color: 'black', fontSize: 10}}\n\
        });\n" % (logger_names, ',\n'.join(v), t, ts, tname ))

	writer.close()	
	writer2.close()

