import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import { DOMSource } from '@cycle/dom/xstream-typings';
const { html } = require('snabbdom-jsx');

export type Sources = {
  DOM: DOMSource,
  WEBSOCKET: Stream<VNode>,
};
export type Sinks = {
  DOM: Stream<VNode>,
  WEBSOCKET: Stream<any>,
}

export default function Main(sources: Sources): Sinks {

  const defaultFrequency = 1;

  const frequencySelection$ = sources
      .DOM
      .select('#freqSelect')
      .events('change')
      .map(event => (event.target as HTMLInputElement).value)
      .startWith( `${defaultFrequency}` );

  const imageFrequencyControl$ = frequencySelection$.map((frequency: any) =>
    <div className="col col-xs-12">
      <div className="well">
        <h4>Choose Image Frequency</h4>
        <input type="range" name="quantity" min="1" max="5" id="freqSelect" value={ frequency } />
        <label>
          { frequency }
        </label>
      </div>
    </div>
  );

  const randomImageUrl = (): string => {
    const random = Math.floor(Math.random() * (1000 - 0 + 1)) + 0;
    return `https://unsplash.it/150/150?image=${random}`;
  };

  const imageClock$ = frequencySelection$.map((frequency: any) => xs.periodic(frequency * 1000)).flatten();

  const imageUrls$ = imageClock$.map(() => randomImageUrl());

  const cumulativeImageUrls$ = imageUrls$.fold((acc: string[], url: string) => acc.push(url) && acc, []);

  const imageTags$ = cumulativeImageUrls$.map((urls: string[]) =>
    <div>
      {
        urls.map(url =>
            <img src={url} />
        )
      }
    </div>
  );

  const imageGallery$ = imageTags$.map(tags =>
      <div className="col col-xs-12">
          <h4>Image Stream...</h4>
          <div>
            { tags }
          </div>
      </div>
  );

  // Websocket stats
  const stats$ = sources.WEBSOCKET
      .startWith('no message yet');

  const statsBlocks$ = stats$.map(stats =>
      <div className="col col-xs-12">
          <h4>Processing stats</h4>
          <div>
              { stats }
          </div>
      </div>
  );

  const vdom$ = xs
      .combine(imageFrequencyControl$, imageGallery$, statsBlocks$)
      .map(([imageFrequencyControl, imageGallery, statsBlocks]) =>
      <div className="container-fluid">
        <div className="row">
          { imageFrequencyControl }
          { imageGallery }
          { statsBlocks }
        </div>
      </div>
  );

  return {
    DOM: vdom$,
    WEBSOCKET: imageUrls$, // send image url request through the websocket
  };

}
