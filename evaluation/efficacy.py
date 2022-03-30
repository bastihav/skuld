import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

plt.close("all")

df = pd.read_csv('efficacy.csv', sep=",", header=0, names=["time_ns", "found_result", "prng"])
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

df.groupby('prng').found_result.value_counts().unstack(0).plot.barh(ax = axes[1]).legend(bbox_to_anchor=(1,1))
df.groupby('prng').boxplot(["time_ns"], showfliers=True, ax=axes[0], rot=90)

plt.subplots_adjust(right=0.55)
axes[1].set_xlabel('Number of Scans')
axes[1].set_ylabel('Match found')

axes[0].set_ylabel('Time in ms')
#axes[0].set_yscale('log')
axes[0].axes.get_xaxis().set_ticklabels(df.prng.drop_duplicates().sort_values())
axes[0].set_title('Analysis time per PRNG')
plt.title("Matches found per PRNG")
#plot = df.plot.bar(x="prng", y="found_result")

#plot = df.boxplot(column="ReadTime2", showfliers=False, labels=[""])

#plot.set_ylabel("Time in ms")

#pd.line(df, linestyle='solid',color='blue')

#pd.show()

plt.show()

print(df)