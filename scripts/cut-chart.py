#!/usr/bin/python

import sys;
from math import sqrt;

def sortedKeys(dict):
	keys = dict.keys()
	keys.sort()
	return keys

def hrf(v):
	suffixes = ['', 'K', 'M', 'G']	
	for s in suffixes:
		if v < 1024 :
			return "%d%s" % (v, s)
		v = int(v / 1024)

if __name__ == '__main__':
	if len(sys.argv) <= 3 :
		print "\tUsage: %s report.txt threads messages" % (sys.argv[0])
		sys.exit(-1)

	width = 1200
	height = 600

	tArg = sys.argv[2]	
	mArg = sys.argv[3]
	threadsArg = int(tArg)	
	msgNoArg = int(mArg)	

	reader = open(sys.argv[1], "r")
	jsname = "%s_%s_%s.js" % (sys.argv[1], tArg, mArg) 
	jswriter = open(jsname, "w")
	htmlwriter = open("%s_%s_%s.html" % (sys.argv[1], tArg, mArg), "w")

	name = ''
	threads = ''
	messages = ''

	loggerMap = {}	
	threadsMap = {}
	messagesMap = {}
	timeMap = {}
	gcMap = {}

	charts = []	

	i = 0	
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


	tp_chart_values = []
	tp_series = 0

	for name in sortedKeys(loggerMap):
		key = name + '_' + tArg + '_' + mArg
		tm = timeMap[key]
		tm.sort()

		v = []
		l = len(tm)

		avg = float(sum(tm) / l )
		s = map(lambda x: (x - avg)**2, tm)
		sigma = sqrt(sum(s) / l)
		tp_series = l

		v.append('%s' % name )
		v.append( max(tm) )
		v.append( avg + sigma )
		v.append( avg - sigma )
		v.append( min(tm) )
		for x in xrange(1, 5):
			v[x] = int( msgNoArg / v[x] )

		tp_chart_values.append(v)
	
	tp_chart = 'throughput_%s_%s' % (tArg, mArg)
	charts.append(tp_chart)


	gc_chart_values = []
	gc_series = 0

	for name in sortedKeys(loggerMap):
		key = name + '_' + tArg + '_' + mArg
		tm = gcMap[key]
		tm.sort()

		v = []

		l = len(tm)

		avg = float(sum(tm) / l )
		s = map(lambda x: (x - avg)**2, tm)
		sigma = sqrt(sum(s) / l)
		gc_series = l

		v.append('%s' % name )
		v.append( min(tm) )
		v.append( avg - sigma )
		v.append( avg + sigma )
		v.append( max(tm) )

		for x in xrange(1, 5):
			# to ms	
			v[x] = int( 1000 * v[x] )
		
		gc_chart_values.append(v)

	gc_chart = 'gc_%s_%s' % (tArg, mArg)
	charts.append(gc_chart)
		
	jswriter.write('function drawChart(){ \n\
	/********* throughput %s %s **************/ \n\
		var data = google.visualization.arrayToDataTable([\n\
%s \n\
		], true); \n\
\n\
		var options = {\n\
		  width: %d, height: %d,\n\
		  title: "Throughput: %s threads / %s msgs / %s series",\n\
		  hAxis: {\n\
				title: "", \n\
				textStyle: {color: "black", fontSize: 14},\n\
				maxAlternation: 1\n\
				},\n\
          vAxis: {title: "messages / ms", gridlines: {color: "#ccc", count: 9}},\n\
          colors: ["#600"],\n\
   		  legend: "none"\n\
     };\n\
\n\
        var chart = new google.visualization.CandlestickChart(document.getElementById("%s"));\n\
        chart.draw(data, options);\n\
	/********* gc %s %s **************/ \n\
		var data = google.visualization.arrayToDataTable([\n\
%s \n\
		], true); \n\
\n\
		var options = {\n\
		  width: %d, height: %d,\n\
		  title: "Total stop the world: %s threads / %s msgs / %s series",\n\
		  hAxis: {\n\
				title: "", \n\
				textStyle: {color: "black", fontSize: 14},\n\
				maxAlternation: 1\n\
				},\n\
          vAxis: {title: "ms", gridlines: {color: "#ccc", count: 9}},\n\
          colors: ["#600"],\n\
		  legend: "none"\n\
        };\n\
\n\
        var chart = new google.visualization.CandlestickChart(document.getElementById("%s"));\n\
        chart.draw(data, options);\n\
}\n' % ( 
			tArg, mArg,		
			",\n".join(map(lambda l: '\t\t\t%s'% l, tp_chart_values)),
			width, height,
			tArg, hrf(msgNoArg), tp_series,
			tp_chart,
			tArg, mArg,		
			",\n".join(map(lambda l: '\t\t\t%s'% l, gc_chart_values)),
			width, height,
			tArg, hrf(msgNoArg), gc_series,
			gc_chart,
		)
	)				
	jswriter.close()	

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

	htmlwriter.close()

