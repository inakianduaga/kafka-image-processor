import xs, {Stream} from 'xstream';
import {VNode} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');

import ImageGallery from './components/ImageGallery';
import Controls from './components/controls/Controls'
import ServerResults from './components/ServerResults';

export type ISources = {
  DOM: DOMSource,
  WEBSOCKET: Stream<any>,
};
export type ISinks = {
  DOM: Stream<any>,
  WEBSOCKET: Stream<any>,
}

export default function Main({DOM, WEBSOCKET}: ISources): ISinks {

  const { DOM: controls$, FILTER: filter$, CLOCK: imageClock$} = Controls({DOM});
  const { DOM: imageGallery$, IMAGE_URLS: imageUrls$ } = ImageGallery({imageClock$});
  const { DOM: serverResults$ } = ServerResults({ WEBSOCKET });

  const imageUrlsJsoned$ = xs
    .combine(imageUrls$, filter$)
    .map(([url, filter]) => ({
      url,
      filter: filter != null ? filter : undefined
    }))
    .map(JSON.stringify)

  const vdom$ = xs
    .combine(controls$, imageGallery$, serverResults$)
    .map(([controls, imageGallery, serverResults]) =>
      <div className='container-fluid p-1'>
        <div className='col-xs-12 col-md-12'>
          <div className='card'>
            <h3 className='card-header'>Controls</h3>
            <div className='card-block'>
              { controls }
            </div>
          </div>
        </div>
        <div className='col-xs-12 col-sm-6 col-md-6'>
          <div className='card'>
            <h3 className='card-header'>Client</h3>
            <div className='card-block'>
              { imageGallery }
            </div>
          </div>
        </div>
        <div className='col-xs-12 col-sm-6 col-md-6'>
          <div className='card'>
            <h3 className='card-header'>Backend</h3>
            <div className='card-block'>
              { serverResults }
            </div>
          </div>
        </div>
      </div>
    );

  return {
    DOM: vdom$,
    WEBSOCKET: imageUrlsJsoned$, // send image url request through the websocket
  };

}
