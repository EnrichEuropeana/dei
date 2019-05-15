package pl.psnc.dei.service;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@Service
public class UIPollingManager {

	private Map<UI, Map<Object, Integer>> pollRequests = new WeakHashMap<>();

	/**
	 * Registers a poll request for the given UI. Sets the pollInterval of this UI to the lowest registered interval.
	 *
	 * @param ui UI for which pollInterval should be added
	 * @param requester object that requested polling
	 * @param pollIntervalInMillis poll interval in milliseconds
	 */
	public void registerPollRequest(UI ui, Object requester, int pollIntervalInMillis) {
		Map<Object, Integer> uiRequests = pollRequests.computeIfAbsent(ui, k -> new HashMap<>());

		uiRequests.put(requester, pollIntervalInMillis);

		setPollInterval(ui);
	}

	/**
	 * Removes a poll request for the given UI (if existent). Sets the pollInterval of this UI to the lowest registered
	 * interval remaining or -1 if no more requests exist for the UI
	 *
	 * @param ui UI for which pollInterval should be removed
	 * @param requester object that requested polling
	 */
	public void unregisterPollRequest(UI ui, Object requester) {
		Map<Object, Integer> uiRequests = pollRequests.get(ui);
		if (uiRequests != null) {
			uiRequests.remove(requester);
			
			if (uiRequests.size() <= 0) {
				pollRequests.remove(ui);
			}
		}
		setPollInterval(ui);
	}

	/**
	 * Removes all poll requests of the given UI and sets the pollInterval to -1
	 *
	 * @param ui UI for which all poll requests should be removed
	 */
	public void unregisterAllPollRequests(UI ui) {
		pollRequests.remove(ui);
		ui.setPollInterval(-1);
	}

	/**
	 * Sets the pollInterval of the given UI to the lowest registered interval time of this UI
	 *
	 * @param ui UI for which pollInterval should be set
	 */
	private void setPollInterval(UI ui) {
		Map<Object, Integer> uiRequests = pollRequests.get(ui);
		if (uiRequests != null) {
			int minInterval = uiRequests.values().stream()
					.mapToInt(v -> v)
					.min()
					.orElse(-1);
			ui.setPollInterval(minInterval);
		} else {
			ui.setPollInterval(-1);
		}
	}
}
