import pandas as pd
import matplotlib.pyplot as plt

# Top 20 URLs bar plot
top_urls = pd.read_csv('results/topurls.txt', sep='\t', names=["url", "count"])
top20 = top_urls.sort_values("count", ascending=False).head(20)
plt.figure(figsize=(12,6))
plt.barh(top20.url, top20["count"])
plt.gca().invert_yaxis()
plt.title("Top 20 NASA URLs (July 1995)")
plt.xlabel("Hits")
plt.tight_layout()
plt.savefig("results/top20_urls.png")
plt.show()

# Hourly errors plot
hourly = pd.read_csv('results/hourly_errors.txt', sep='\t', names=["hour", "ok", "e404", "e500", "err_pct"])
hourly["hour"] = pd.to_numeric(hourly["hour"])
plt.figure(figsize=(10,5))
plt.plot(hourly["hour"], hourly["err_pct"].str.rstrip('%').astype(float), marker='o')
plt.title("Hourly HTTP Error Rate (%)")
plt.xlabel("Hour (0-23)")
plt.ylabel("Error Rate (%)")
plt.xticks(range(0,24))
plt.grid(True)
plt.tight_layout()
plt.savefig("results/hourly_errors.png")
plt.show()