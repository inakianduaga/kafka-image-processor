import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');

export type ISources = {
  WEBSOCKET: Stream<VNode>,
};

export type ISinks = {
  DOM: Stream<VNode>,
}

const ServerResults = ({WEBSOCKET}: ISources): ISinks => {

  // Websocket stats
  const serverResults$ = WEBSOCKET
    .startWith('nothing from the server yet...');

  return {
    DOM: serverResults$,
  };
};

export default ServerResults;
