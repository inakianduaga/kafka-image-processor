import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');
import Config from '../services/Config';

type ISources = {
  DOM: DOMSource,
  PROCESSING: Stream<boolean>
};

export type ISinks = {
  DOM: Stream<JSX.Element>,
  CLOCK: Stream<number>
}

const FrequencyControl = (sources: ISources): ISinks =>{

  const frequencySelection$ = sources
    .DOM
    .select('#freqSelect')
    .events('change')
    .map(event => (event.target as HTMLInputElement).value)
    .map(freq => parseInt(freq, 1))
    .startWith(Config.defaults.images.frequency)


  const processStart$ = sources.PROCESSING.filter(enabled => enabled);
  const processStop$ = sources.PROCESSING.filter(enabled => !enabled).drop(1);

  const imageClock$ = xs.combine(processStart$, frequencySelection$)
    .map(([processStart, frequency]) => xs.periodic(frequency * 1000))
    .flatten()
    .endWhen(processStop$)

  // const imageClock$ = processStart$.map((frequency: any) => xs.periodic(frequency * 1000)).flatten();

  const imageFrequencyControl$ = frequencySelection$.map((frequency: any) =>
    <div className='col col-xs-12 mb-1'>
      <div className='well'>
        <h5>Upload Frequency</h5>
        <input type='range' name='quantity' min='1' max='5' id='freqSelect' value={ frequency } style={{ marginRight: '1em'}}/>
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
