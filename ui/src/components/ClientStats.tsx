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
      <div className="col col-xs-12">
        <h4>Processing stats</h4>
        <div className="row">
          <div className="col-xs-12 col-md-6">
            <h5>Client uploads</h5>
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
