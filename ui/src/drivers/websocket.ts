import xs, {Stream} from 'xstream';

/**
 * The signature of a two-way cycleJS driver, which is a basically a stream transformation
 */
export type ICycleJSTwoWayDriver = (outgoing$: Stream<Event>) => Stream<any>;

/**
 * https://cycle.js.org/drivers.html
 */
function makeWebSocketDriver(hostnameAndPort: string): ICycleJSTwoWayDriver {

  const ws = new WebSocket(`ws://${hostnameAndPort}`);
  const queue: Event[] = [];

  // When the websocket connection opens, fire all queued messages
  ws.onopen = () => queue.forEach(out => ws.send(out));

  return (outgoing$: Stream<Event>) => {

    // Register event listener on input stream that will push it through the websocket
    outgoing$.addListener({
      next: outgoing => {
        if (ws.readyState === 1) {
          ws.send(outgoing);
        } else {
          queue.push(outgoing);
        }
      },
      error: () => { /**/ },
      complete: () => { /**/ },
    });

    // Create a stream source that we will be able to register to that emits every time
    // we get a new websocket incoming message
    return xs.create({
      start: listener => {
        ws.onmessage = (msg: MessageEvent) => listener.next(msg);
        ws.onerror = (err: Event) => listener.error(err);
        ws.onclose = () => listener.complete();
      },
      stop: () => { /**/ },
    });
  }

}

export default makeWebSocketDriver;
