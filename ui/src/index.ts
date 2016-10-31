import {run} from '@cycle/xstream-run';
import {makeDOMDriver} from '@cycle/dom';
import Main from './main';

const main = Main;

run(main, {
  DOM: makeDOMDriver('#app')
});
