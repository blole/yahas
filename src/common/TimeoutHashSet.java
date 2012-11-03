package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class TimeoutHashSet<K> {
	private HashMap<K, Long> hashMap = new HashMap<>();

	public TimeoutHashSet(long timeout_ms, Action<K> onRemoval) {
		forkReportingDataNodeDisconnections(timeout_ms, onRemoval);
	}

	/**
	 * Adds an element to the set, or if it is already in the set, renew that
	 * elements timeout.
	 * 
	 * @param element
	 * @return True if the set didn't already contain the element, that is: it
	 *         was added
	 */
	public boolean addOrRefresh(K element) {
		return hashMap.put(element, System.currentTimeMillis()) == null;
	}

	/**
	 * Stops tracking the given element, without executing the given
	 * {@code onRemoval}.
	 * 
	 * @param element
	 */
	public void remove(K element) {
		hashMap.remove(element);
	}
	
	public boolean contains(K element) {
		return hashMap.containsKey(element);
	}

	private void forkReportingDataNodeDisconnections(final long timeout_ms,
			final Action<K> onRemoval) {
		new Thread() {
			@Override
			public void run() {

				ArrayList<K> toBeRemoved = new ArrayList<>();

				long soonestTimeout = 0;

				while (true) {
					try {
						long timeTillNextTimeout = soonestTimeout
								- System.currentTimeMillis();
						if (timeTillNextTimeout <= 0)
							Thread.sleep(timeout_ms);
						else
							Thread.sleep(timeTillNextTimeout);
					} catch (InterruptedException e) {
					}

					long oldestAllowedTime = System.currentTimeMillis()
							- timeout_ms;
					for (Entry<K, Long> e : hashMap.entrySet()) {
						if (e.getValue() < oldestAllowedTime)
							toBeRemoved.add(e.getKey());
						else
							soonestTimeout = Math.min(soonestTimeout,
									e.getValue());
					}

					for (K element : toBeRemoved) {
						hashMap.remove(element);
						onRemoval.execute(element);
					}

					toBeRemoved.clear();
				}
			}
		}.start();
	}
}