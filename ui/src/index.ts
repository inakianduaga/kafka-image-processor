import {run} from '@cycle/xstream-run';
import {makeDOMDriver} from '@cycle/dom';
import Main from './main';
import makeWebSocketDriver from './drivers/websocket';
import Config from './services/Config';

const main = Main;

const drivers = {
  DOM: makeDOMDriver('#app'),
  WEBSOCKET: makeWebSocketDriver(Config.websocketEndpoint),
};

run(main, drivers);
