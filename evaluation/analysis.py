import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.ticker as mticker
plt.close("all")

df = pd.read_csv('analysis.csv', sep=",", header=0, names=["time_ns", "positive", "false_positive", "negative", "false_negative", "actual_prng", "actual_seed", "expected_prng", "expected_seed", "check_manually"])
print(df.head())
print(df.columns)
#df["Read Time No Node"] = df["Read Time No Node"].apply(lambda x: x/1000000)
#df["Read Time Node"] = [x if df[" foundNode"][ind] == True else np.nan for ind, x in enumerate(df["Read Time No Node"])]

print("done")
#plt.figure()

#df.plot.line()

#plot = df.boxplot(["time_ns"], showfliers=False)
#df["found_result"] = df["found_result"].astype(int)

fig, axes = plt.subplots(nrows=1,ncols=2,figsize=(12,6))
df["time_ns"] = df["time_ns"].apply(lambda x: x/1000000)

p = df.groupby("positive").positive.value_counts().get(True, 0)
n = df.groupby("negative").negative.value_counts().get(True, 0)
fp = df.groupby("false_positive").false_positive.value_counts().get(True, 0)
fn = df.groupby("false_negative").false_negative.value_counts().get(True, 0)

print(p)
print(n)
print(fp)
print(fn)

axes[1].bar(["True Positive","False Positive","True Negative","False Negative"], [p,fp,n,fn], color=['green', 'red', 'green', 'red'])

#df.plot(y="positive", kind='bar')
#df[["positive", "negative", "false_positive", "false_negative"]].value_counts().plot.bar().legend(bbox_to_anchor=(1,1))

#df.groupby('prng').found_result.value_counts().unstack(0).plot.barh(ax = axes[1]).legend(bbox_to_anchor=(1,1))

tp = df[df.positive.eq(True)]["time_ns"]
tn = df[df.negative.eq(True)]["time_ns"]
tfp = df[df.false_positive.eq(True)]["time_ns"]
tfn = df[df.false_negative.eq(True)]["time_ns"]
print(tp)
axes[0].boxplot([tp,tfp,tn,tfn], showfliers=True)
axes[0].set_xticks([1, 2, 3, 4], ["True Positive","False Positive","True Negative","False Negative"], rotation = 45)
axes[0].set_yscale('log')
axes[0].yaxis.set_major_formatter(mticker.ScalarFormatter())
axes[0].set_ylabel('Time in ms')
axes[0].set_xlabel('Result')
axes[0].set_yticks([100, 200, 300, 400, 600, 800, 1000, 1500, 2000, 3000, 4000, 5000, 6000])

#plt.subplots_adjust(right=0.55)
axes[1].set_ylabel('Number of Handshakes')
axes[1].set_xlabel('Result')
axes[1].set_xticks([0,1,2,3], ["True Positive","False Positive","True Negative","False Negative"], rotation = 45)
#axes[1].set_yscale('log')
axes[1].yaxis.set_major_formatter(mticker.ScalarFormatter())


#axes[0].set_ylabel('Time in ms')

#axes[0].axes.get_xaxis().set_ticklabels(df.prng.drop_duplicates().sort_values())
#axes[0].set_title('Analysis time per PRNG')
#plt.title("Matches found per PRNG")
#plot = df.plot.bar(x="prng", y="found_result")

#plot = df.boxplot(column="ReadTime2", showfliers=False, labels=[""])

#plot.set_ylabel("Time in ms")

#pd.line(df, linestyle='solid',color='blue')

#pd.show()

plt.show()

print(df)