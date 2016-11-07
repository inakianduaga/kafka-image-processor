import xs, {Stream} from 'xstream';
import {VNode} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');

import ImageGallery from './components/ImageGallery';
import ClientStats from './components/ClientStats';
import FrequencyControl from './components/FrequencyControl';
import ServerResults from './components/ServerResults';

export type ISources = {
  DOM: DOMSource,
  WEBSOCKET: Stream<any>,
};
export type ISinks = {
  DOM: Stream<VNode>,
  WEBSOCKET: Stream<any>,
}

export default function Main({DOM, WEBSOCKET}: ISources): ISinks {

  const { DOM: imageFrequencyControl$, CLOCK: imageClock$} = FrequencyControl({DOM});
  const { DOM: imageGallery$, IMAGE_URLS: imageUrls$ } = ImageGallery({imageClock$});
  const { DOM: clientStats$} = ClientStats({imageUrls$});
  const { DOM: serverResults$ } = ServerResults({WEBSOCKET});

  const imageUrlsJsoned$ = imageUrls$.map(url => JSON.stringify(url));

  const vdom$ = xs
    .combine(imageFrequencyControl$, imageGallery$, clientStats$, serverResults$)
    .map(([imageFrequencyControl, imageGallery, clientStats, serverResults]) =>
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
            { serverResults }
          </div>
        </div>
      </div>
    );

  return {
    DOM: vdom$,
    WEBSOCKET: imageUrlsJsoned$, // send image url request through the websocket
  };

}
