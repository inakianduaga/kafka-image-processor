import xs, {Stream} from 'xstream';
import {VNode} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');

import ImageGallery from './components/ImageGallery';
import ClientStats from './components/ClientStats';
import FrequencyControl from './components/FrequencyControl';
import ServerResults from './components/ServerResults';
import FilterSelection from './components/FilterSelection';
import ProcessingControl from './components/ProcessingControl';

export type ISources = {
  DOM: DOMSource,
  WEBSOCKET: Stream<any>,
};
export type ISinks = {
  DOM: Stream<any>,
  WEBSOCKET: Stream<any>,
}

export default function Main({DOM, WEBSOCKET}: ISources): ISinks {

  const { DOM: processingControl$, PROCESSING: processing$} = ProcessingControl({ DOM });
  const { DOM: imageFrequencyControl$, CLOCK: imageClock$} = FrequencyControl({DOM, PROCESSING: processing$});
  const { DOM: imageGallery$, IMAGE_URLS: imageUrls$ } = ImageGallery({imageClock$});
  const { DOM: clientStats$} = ClientStats({imageUrls$});
  const { DOM: serverResults$ } = ServerResults({ WEBSOCKET });
  const { DOM: filterSelectionControl$, FILTER: filter$} = FilterSelection({DOM});

  const imageUrlsJsoned$ = xs
    .combine(imageUrls$, filter$)
    .map(([url, filter]) => ({
      url,
      filter: filter != null ? filter : undefined
    }))
    .map(JSON.stringify)

  const vdom$ = xs
    .combine(imageFrequencyControl$, imageGallery$, clientStats$, serverResults$, filterSelectionControl$, processingControl$)
    .map(([imageFrequencyControl, imageGallery, clientStats, serverResults, filterSelection, processingControl]) =>
      <div className='container-fluid p-1'>
        <div className='row'>
          <div className='col-xs-6 col-md-6'>
            <div className='card'>
              <h3 className='card-header'>Client</h3>
              <div className='card-block'>
                <div className='row'>
                  { processingControl }
                  <div className='col-xs-12 col-md-6'>
                    <div className='row'>
                      { imageFrequencyControl }
                    </div>
                  </div>
                  <div className='col-xs-12 col-md-6'>
                    <div className='row'>
                      { filterSelection}
                    </div>
                  </div>
                  { clientStats }
                  { imageGallery }
                </div>
              </div>
            </div>
          </div>
          <div className='col-xs-6 col-md-6'>
            <div className='card'>
              <h3 className='card-header'>Backend</h3>
              <div className='card-block'>
                { serverResults }
              </div>
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
