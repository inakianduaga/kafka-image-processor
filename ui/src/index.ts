import {run} from '@cycle/xstream-run';
import {makeDOMDriver} from '@cycle/dom';
import Main from './main';
import makeWebSocketDriver from './drivers/websocket';

const main = Main;

run(main, {
  DOM: makeDOMDriver('#app'),
  WEBSOCKET: makeWebSocketDriver(process.env.BACKEND_ENDPOINT),
});
