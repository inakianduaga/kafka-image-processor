import xs, {Stream} from 'xstream';

/**
 * The signature of a two-way cycleJS driver, which is a basically a stream transformation
 */
export type ICycleJSTwoWayDriver = (outgoing$: Stream<Event>) => Stream<any>;

/**
 * https://cycle.js.org/drivers.html
 *
 * This wrapper function will return a CycleJS driver, which
 */
function makeWebSocketDriver(hostnameAndPort: string): ICycleJSTwoWayDriver {

  let ws = new WebSocket(`ws://${hostnameAndPort}`);
  console.log(ws);
  ws.send({init:'done, testing123'});

  return (outgoing$: Stream<Event>) => {

    // Register event listener on input stream that will push it through the websocket
    outgoing$.addListener({
      next: outgoing => ws.send(outgoing),
      error: () => {},
      complete: () => {},
    });

    // Create a stream source that we will be able to register to that emits every time
    // we get a new websocket incoming message
    return xs.create({
      start: listener => {
        ws.onmessage = (msg: MessageEvent) => listener.next(msg);
        ws.onerror = (err: Event) => listener.error(err);
      },
      stop: () => {
      },
    });
  }

}

export default makeWebSocketDriver;
