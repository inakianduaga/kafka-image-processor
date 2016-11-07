import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');

import ImageGallery from './components/ImageGallery';
import ClientStats from './components/ClientStats';
import FrequencyControl from './components/FrequencyControl';

export type Sources = {
  DOM: DOMSource,
  WEBSOCKET: Stream<VNode>,
};
export type Sinks = {
  DOM: Stream<VNode>,
  WEBSOCKET: Stream<any>,
}

export default function Main({DOM, WEBSOCKET}: Sources): Sinks {

  const { DOM: imageFrequencyControl$, CLOCK: imageClock$} = FrequencyControl({DOM});
  const { DOM: imageGallery$, IMAGE_URLS: imageUrls$ } = ImageGallery({imageClock$});
  const { DOM: clientStats$} = ClientStats({imageUrls$});

  // Websocket stats
  const serverResults$ = WEBSOCKET
    .startWith('no message yet');

  const imageUrlsJsoned$ = imageUrls$.map(url => JSON.stringify(url));

  const vdom$ = xs
    .combine(imageFrequencyControl$, imageGallery$, clientStats$)
    .map(([imageFrequencyControl, imageGallery, clientStats]) =>
      <div className="container-fluid">
        <div className="row">
          <div className="col-xs-6 col-md-6">
            <div className="row">
              { imageFrequencyControl }
              { clientStats }
              { imageGallery }
            </div>
          </div>
          <div className="col-xs-6 col-md-6">
            HERE GO SERVER SIDE RESULTS
          </div>
        </div>
      </div>
    );

  return {
    DOM: vdom$,
    WEBSOCKET: imageUrlsJsoned$, // send image url request through the websocket
  };

}
