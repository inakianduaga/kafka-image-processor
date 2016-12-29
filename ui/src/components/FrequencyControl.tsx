import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');
import Config from '../services/Config';

type ISources = {
  DOM: DOMSource,
};

export type ISinks = {
  DOM: Stream<VNode>,
  CLOCK: Stream<number>
}

const FrequencyControl = (sources: ISources): ISinks =>{

  const frequencySelection$ = sources
    .DOM
    .select('#freqSelect')
    .events('change')
    .map(event => (event.target as HTMLInputElement).value)
    .startWith(`${Config.defaults.images.frequency}`);

  const imageClock$ = frequencySelection$.map((frequency: any) => xs.periodic(frequency * 1000)).flatten();

  const imageFrequencyControl$ = frequencySelection$.map((frequency: any) =>
    <div className="col col-xs-12">
      <div className="well">
        <h4>Image Upload Frequency</h4>
        <input type="range" name="quantity" min="1" max="5" id="freqSelect" value={ frequency }/>
        <label>
          every { frequency }s
        </label>
      </div>
    </div>
  );

  return {
    DOM: imageFrequencyControl$,
    CLOCK: imageClock$
  }
};

export default FrequencyControl;
