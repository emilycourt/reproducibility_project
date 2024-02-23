import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.ticker as tick

names=["NP", "queries", "parsingTime", "filteringTime"]
data = pd.read_csv("/home/repro/results/table.txt", sep=" ", names=names, index_col=False)
data = data.assign(MQPT=data["filteringTime"] - data["parsingTime"])

MQPT = data[['NP', 'queries', 'MQPT']]
plot_data = MQPT.set_index(['queries', 'NP']).unstack()['MQPT'].iloc[:, [2, 1, 0]]


xlabel = 'Number of queries (x1000)'
ylabel = 'MQPT (ms)'
xticks = [1000, 10000, 50000, 100000, 150000, 200000]
style = ['d', 's', '^']
linestyle='-'
color = ['black', 'grey', 'dimgrey']

def x_fmt(x, y):
	return int(x / 1000)

plot = plot_data.plot(xlabel=xlabel, ylabel=ylabel, xticks=xticks, style=style, linestyle=linestyle, color=color)

plot.xaxis.set_major_formatter(tick.FuncFormatter(x_fmt))

plt.savefig('replication_figure.png')
