import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import { DOMSource } from '@cycle/dom/xstream-typings';
const { html } = require('snabbdom-jsx');

export type Sources = {
  DOM: DOMSource,
};
export type Sinks = {
  DOM: Stream<VNode>,
}

export default function Main(sources: Sources): Sinks {

  const defaultFrequency = 1;
  const defaultFrequencyEvent = new CustomEvent('change', { detail: defaultFrequency });
  const frequencySelection$ = sources.DOM.select('#freqSelect').events('change')!.startWith( defaultFrequencyEvent );

  const imageFetchFrequency$ = frequencySelection$.map((event: any) => {
    const value = event.detail || (event.target && event.target.value);
    return <div>
        <input type="range" name="quantity" min="1" max="5" id="freqSelect" value={ value } />
        <span>
          { value }
        </span>
      </div>;
    }
  );

  return {
    DOM: imageFetchFrequency$
  };

}
