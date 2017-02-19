import xs, {Stream} from 'xstream';
import {VNode} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');

import ClientStats from './ClientStats';
import FrequencyControl from './FrequencyControl';
import ImageGallery from '../ImageGallery';
import FilterSelection, { IFilterSelection } from './FilterSelection';
import ProcessingControl from './ProcessingControl';

export type ISources = {
  DOM: DOMSource,
};

export type ISinks = {
  DOM: Stream<any>,
  FILTER: Stream<IFilterSelection>,
  CLOCK: Stream<number>
}

export default function Controls({DOM}: ISources): ISinks {

  const { DOM: processingControl$, PROCESSING: processing$} = ProcessingControl({ DOM });
  const { DOM: imageFrequencyControl$, CLOCK: imageClock$} = FrequencyControl({DOM, PROCESSING: processing$});
  const { IMAGE_URLS: imageUrls$ } = ImageGallery({imageClock$});
  const { DOM: clientStats$} = ClientStats({imageUrls$});
  const { DOM: filterSelectionControl$, FILTER: filter$} = FilterSelection({DOM});

  const vdom$ = xs
    .combine(imageFrequencyControl$, clientStats$, filterSelectionControl$, processingControl$)
    .map(([imageFrequencyControl, clientStats, filterSelection, processingControl]) =>
      <div className='row'>
        { processingControl }
        <div className='col-xs-12 col-sm-6 col-md-6'>
          <div className='row'>
            { imageFrequencyControl }
          </div>
        </div>
        <div className='col-xs-12 col-sm-6 col-md-6'>
          <div className='row'>
            { filterSelection}
          </div>
        </div>
        { clientStats }
      </div>
    );

  return {
    DOM: vdom$,
    FILTER: filter$,
    CLOCK: imageClock$
  };

}
