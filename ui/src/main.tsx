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

  const frequencySelection$ = sources
      .DOM
      .select('#freqSelect')
      .events('change')
      .startWith( new CustomEvent('change', {
        detail: defaultFrequency
      }));

  const imageFrequencyControl$ = frequencySelection$.map((event: any) => {
    const value = event.detail || (event.target && event.target.value);
    return (
        <div className="col col-xs-12">
          <div className="well">
            <h4>Choose Image Frequency</h4>
            <input type="range" name="quantity" min="1" max="5" id="freqSelect" value={ value } />
            <label>
              { value }
            </label>
          </div>
        </div>
    );
  });

  const randomImageUrl = (): string => {
    const random = Math.floor(Math.random() * (1000 - 0 + 1)) + 0;
    return `https://unsplash.it/150/150?image=${random}`;
  };

  const imageClock$ = frequencySelection$.map((event: any) => {
    const value = event.detail || (event.target && event.target.value);
    return xs.periodic(value * 1000)
  }).flatten();

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

  // TODO: USE a websocket driver to push messages through the websocket
  // imageProcessRequest = $imageUrls$.map(url => ({
  //   url,
  //   method: 'POST'
  // }));


  const vdom$ = xs
      .combine(imageFrequencyControl$, imageGallery$)
      .map(([imageFrequencyControl, imageGallery]) =>
      <div className="container-fluid">
        <div className="row">
          { imageFrequencyControl }
          { imageGallery }
        </div>
      </div>
  );


  return {
    DOM: vdom$
  };

}
