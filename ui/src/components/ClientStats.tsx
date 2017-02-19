import xs, {Stream} from 'xstream';
import {VNode} from '@cycle/dom';
const {html} = require('snabbdom-jsx');
import Config from '../services/Config';

export type ISources = {
  imageUrls$: Stream<string>,
};

export type ISinks = {
  DOM: Stream<any>,
}

const ClientStats = ({imageUrls$}: ISources): ISinks => {

  const { defaults: { images: { maxUpload: MAX_UPLOAD_IMAGES } } } = Config;

  // Client stats
  const uploadCount$ = imageUrls$.fold((count) => count + 1, 0);
  const uploadPercentage$ = uploadCount$.map(count => 100 * count / MAX_UPLOAD_IMAGES);

  const clientStats$ = xs.combine(uploadCount$, uploadPercentage$)
    .map(([uploadCount, uploadPercentage]) =>
      <div className='col col-xs-12 mb-1'>
        <h5>Processing stats</h5>
        <div className='row'>
          <div className='col-xs-12 col-md-6'>
            <h6>Client uploads</h6>
            <p>
              { uploadCount } / { MAX_UPLOAD_IMAGES } | { uploadPercentage}%
            </p>
          </div>
        </div>
      </div>
    );

  return {
    DOM: clientStats$
  }
};

export default ClientStats;
