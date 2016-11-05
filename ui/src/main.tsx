import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');

export type Sources = {
  DOM: DOMSource,
  WEBSOCKET: Stream<VNode>,
};
export type Sinks = {
  DOM: Stream<VNode>,
  WEBSOCKET: Stream<any>,
}

const defaultFrequency = 3;
const maxUploadImages = 5;
const imageSize = 100;

export default function Main(sources: Sources): Sinks {

  const frequencySelection$ = sources
    .DOM
    .select('#freqSelect')
    .events('change')
    .map(event => (event.target as HTMLInputElement).value)
    .startWith(`${defaultFrequency}`);

  const imageFrequencyControl$ = frequencySelection$.map((frequency: any) =>
    <div className="col col-xs-12">
      <div className="well">
        <h4>Image Upload Frequency</h4>
        <input type="range" name="quantity" min="1" max="5" id="freqSelect" value={ frequency }/>
        <label>
          every { frequency }s
        </label>
      </div>
    </div>
  );

  const randomImageUrl = (): string => {
    const random = Math.floor(Math.random() * (1000 - 0 + 1)) + 0;
    return `https://unsplash.it/${imageSize}/${imageSize}?image=${random}`;
  };

  const imageClock$ = frequencySelection$.map((frequency: any) => xs.periodic(frequency * 1000)).flatten();

  const imageClockCapped$ = imageClock$.take(maxUploadImages);

  const imageUrls$ = imageClockCapped$.map(() => randomImageUrl());

  const cumulativeImageUrls$ = imageUrls$.fold((acc: string[], url: string) => acc.push(url) && acc, []);

  const imageTags$ = cumulativeImageUrls$.map((urls: string[]) =>
    <div>
      {
        urls.map(url =>
          <img src={url} style={{ border: "1px solid #ddd" }} height={imageSize} width={imageSize}/>
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
  const serverResults$ = sources.WEBSOCKET
    .startWith('no message yet');

  // Client stats
  const uploadCount$ = imageUrls$.fold((count) => count + 1, 0);
  const uploadPercentage$ = uploadCount$.map(count => 100 * count / maxUploadImages);

  const clientStats$ = xs.combine(uploadCount$, uploadPercentage$)
    .map(([uploadCount, uploadPercentage]) =>
      <div className="col col-xs-12">
        <h4>Processing stats</h4>
        <div className="row">
          <div className="col-xs-12 col-md-6">
            <h5>Client uploads</h5>
            <p>
              { uploadCount } / { maxUploadImages } | { uploadPercentage}%
            </p>
          </div>
        </div>
      </div>
  );

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
    WEBSOCKET: imageUrls$, // send image url request through the websocket
  };

}
