const refreshButton = document.querySelector('#refresh-button');
const statusChip = document.querySelector('#status-chip');
const statusValue = document.querySelector('#status-value');
const statusNote = document.querySelector('#status-note');
const lastSignal = document.querySelector('#last-signal');
const endpointStatus = document.querySelector('#endpoint-status');
const footerState = document.querySelector('#footer-state');
const telemetryValue = document.querySelector('#telemetry-value');
const telemetryNote = document.querySelector('#telemetry-note');
const signalList = document.querySelector('#signal-list');
const healthRefreshIntervalMs = 30_000;
const telemetryRefreshIntervalMs = 60_000;

function setConnectionState(healthy, timestamp) {
    const signalTime = timestamp ? new Date(timestamp) : new Date();
    const formattedTime = signalTime.toLocaleTimeString([], { hour12: false });

    statusChip.textContent = healthy ? 'Operational' : 'Unavailable';
    statusChip.style.color = healthy ? '#b8e8d7' : '#ef775f';
    statusValue.innerHTML = healthy ? 'All systems nominal' : 'Signal unavailable';
    statusNote.textContent = healthy ? 'Health endpoint responded successfully' : 'The backend could not be reached';
    lastSignal.textContent = healthy ? formattedTime : '--:--:--';
    endpointStatus.textContent = healthy ? '200 OK' : 'No response';
    endpointStatus.style.color = healthy ? '#087f73' : '#ef775f';
    footerState.textContent = healthy ? `signal received at ${formattedTime}` : 'waiting for signal';
}

async function checkHealth() {
    statusChip.textContent = 'Checking';
    statusChip.style.color = '#f4c95d';
    statusValue.innerHTML = 'Connecting<span class="blink">...</span>';
    statusNote.textContent = 'Contacting the local health endpoint';
    endpointStatus.textContent = 'Waiting';

    try {
        const response = await fetch('/api/v1/health', { headers: { Accept: 'application/json' } });
        if (!response.ok) throw new Error(`Health check returned ${response.status}`);
        const health = await response.json();
        setConnectionState(health.status === 'ok', health.timestamp);
    } catch (error) {
        setConnectionState(false);
    }
}

async function loadTelemetry() {
    try {
        const response = await fetch('/api/v1/signals', { headers: { Accept: 'application/json' } });
        if (!response.ok) throw new Error(`Signals request returned ${response.status}`);
        const signals = await response.json();
        const categories = [...new Set(signals.map(signal => signal.category))];
        telemetryValue.textContent = `${signals.length} signals`;
        telemetryNote.textContent = categories.length
            ? categories.join(' · ')
            : 'No signals available';
        renderSignals(signals);
    } catch (error) {
        telemetryValue.textContent = 'Unavailable';
        telemetryNote.textContent = 'Signal provider could not be reached';
        signalList.replaceChildren(createFeedMessage('Signal provider could not be reached'));
    }
}

function createFeedMessage(message) {
    const element = document.createElement('p');
    element.className = 'panel-footnote';
    element.textContent = message;
    return element;
}

function renderSignals(signals) {
    signalList.replaceChildren();
    if (!signals.length) {
        signalList.append(createFeedMessage('No signals available'));
        return;
    }

    signals.slice(0, 8).forEach(signal => {
        const item = document.createElement('article');
        item.className = 'signal-item';

        const meta = document.createElement('div');
        meta.className = 'signal-meta';
        const category = document.createElement('span');
        category.className = 'signal-category';
        category.textContent = signal.category;
        const severity = document.createElement('span');
        severity.className = `signal-severity severity-${signal.severity.toLowerCase()}`;
        severity.textContent = signal.severity;
        meta.append(category, severity);

        const title = document.createElement('p');
        title.className = 'signal-title';
        title.textContent = signal.title;

        const details = document.createElement('div');
        details.className = 'signal-details';
        const source = document.createElement('span');
        source.textContent = signal.source;
        const observedAt = document.createElement('time');
        observedAt.dateTime = signal.observedAt;
        observedAt.textContent = new Date(signal.observedAt).toLocaleString([], {
            dateStyle: 'medium',
            timeStyle: 'short'
        });
        details.append(source, observedAt);

        item.append(meta, title, details);
        if (signal.sourceUrl) {
            const link = document.createElement('a');
            link.className = 'signal-link';
            link.href = signal.sourceUrl;
            link.target = '_blank';
            link.rel = 'noreferrer';
            link.textContent = 'Open source';
            item.append(link);
        }
        signalList.append(item);
    });
}

async function refreshDashboard() {
    await Promise.all([checkHealth(), loadTelemetry()]);
}

refreshButton.addEventListener('click', refreshDashboard);
checkHealth();
loadTelemetry();
setInterval(checkHealth, healthRefreshIntervalMs);
setInterval(loadTelemetry, telemetryRefreshIntervalMs);
