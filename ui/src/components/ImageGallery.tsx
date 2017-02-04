import xs, {Stream} from 'xstream';
import {VNode} from '@cycle/dom';
const {html} = require('snabbdom-jsx');
import Config from '../services/Config';

type ISources = {
  imageClock$: Stream<number>,
};

type ISinks = {
  DOM: Stream<any>,
  IMAGE_URLS: Stream<string>,
};

const ImageGallery = ({imageClock$}: ISources): ISinks => {

  const { defaults: { images: { size : IMAGE_SIZE, maxUpload: MAX_UPLOAD_IMAGES } } } = Config;

  const randomImageUrl = (): string => {
    const random = Math.floor(Math.random() * (1000 - 0 + 1)) + 0;
    return `https://unsplash.it/${IMAGE_SIZE}/${IMAGE_SIZE}?image=${random}`;
  };

  const imageClockCapped$ = imageClock$.take(MAX_UPLOAD_IMAGES);

  const imageUrls$ = imageClockCapped$.map(() => randomImageUrl());

  const cumulativeImageUrls$ = imageUrls$.fold((acc: string[], url: string) => acc.push(url) && acc, []);

  const imageTags$ = cumulativeImageUrls$.map((urls: string[]) =>
    <div>
      {
        urls.map(url =>
          <img src={url} style={{ border: "1px solid #ddd" }} height={IMAGE_SIZE} width={IMAGE_SIZE}/>
        )
      }
    </div>
  );

  const imageGallery$ = imageTags$.map(tags =>
    <div className="col col-xs-12 mb-1">
      <h4>Image Stream...</h4>
      <div>
        { tags }
      </div>
    </div>
  );

  return {
    DOM: imageGallery$,
    IMAGE_URLS: imageUrls$,
  }
};

export default ImageGallery;
