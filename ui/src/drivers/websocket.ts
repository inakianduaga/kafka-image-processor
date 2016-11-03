import xs, {Stream} from 'xstream';

/**
 * https://cycle.js.org/drivers.html
 */
function makeWebSocketDriver(hostnameAndPort: string) {

    let ws = new WebSocket(`ws://${hostnameAndPort}`);

    function sockDriver(outgoing$: Stream<Event>) {

        outgoing$.addListener({
            next: outgoing => ws.send(outgoing),
            error: () => {},
            complete: () => {},
        });

        return xs.create({
            start: listener => {
                ws.onmessage = (msg: MessageEvent) => listener.next(msg);
                ws.onerror = (err: Event) => listener.error(err);
            },
            stop: () => {},
        });
    }

    return sockDriver;
}

export default makeWebSocketDriver;
