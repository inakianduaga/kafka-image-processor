import xs, { Stream, MemoryStream } from 'xstream';
import { VNode, CycleDOMEvent } from '@cycle/dom';
import { DOMSource } from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');
import Config from '../../services/Config';

type ISources = {
  DOM: DOMSource,
};

export type ISinks = {
  DOM: Stream<JSX.Element>,
  PROCESSING: Stream<boolean>
}

const ProcessingControl = (sources: ISources): ISinks => {

  const clicks$ = sources
    .DOM
    .select('#processingToggle')
    .events('click');

  const processing$ = clicks$.fold((acc: boolean) => !acc, false);

  const processingControl$ = processing$.map(isEnabled =>
    <div className='col col-xs-12 mb-1'>
      {
        <button
          type='button'
          className={`btn btn-${isEnabled ? 'inverse' : 'success'}`}
          id='processingToggle'
          style={{ width: '100%' }}
        >
          {isEnabled ? 'Pause' : 'Start!'}
        </button>
      }
    </div>
  );

  return {
    DOM: processingControl$,
    PROCESSING: processing$
  }
};

export default ProcessingControl;
