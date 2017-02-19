import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');
import Config from '../services/Config';

export type ISources = {
  WEBSOCKET: Stream<MessageEvent>,
};

export type ISinks = {
  DOM: Stream<VNode>,
}

export interface WebsocketMessageType {
  type: string
}

interface ImageProcessedMessage extends WebsocketMessageType {
  content: string,
  format: 'PNG' | 'JPG' | 'GIF'
  encoding: 'base64',
  type: 'PROCESSED_IMAGE'
}

const { defaults: { images: { size : IMAGE_SIZE } } } = Config;

const ServerResults = ({WEBSOCKET}: ISources): ISinks => {

  const processedImagesFromWebsocket$ = WEBSOCKET
    .map(message => JSON.parse(message.data))
    .filter(message => message.type === 'PROCESSED_IMAGE')
    .map( (message: ImageProcessedMessage) => {
      const image = new Image(IMAGE_SIZE, IMAGE_SIZE);
      image.src = `data:image/${message.format.toLowerCase};base64, ${message.content}`;
      return image;
    })

  const cumulativeProcessedImages$  = processedImagesFromWebsocket$
    .fold((acc: HTMLImageElement[], image) => acc.push(image) && acc, []);

  const processedImageTags$ = cumulativeProcessedImages$
    .map((images: HTMLImageElement[]) =>
      <div>
        {
          images.map(image =>
            <img src={image.src} style={{ border: '1px solid #ddd' }} height={image.height} width={image.width} />
          )
        }
      </div>
    );

    const serverResults$ = processedImageTags$.map(tags =>
    <div className='col col-xs-12 mb-1'>
      <h4>Processed Stream...</h4>
      { tags }
    </div>
  );

  return {
    DOM: serverResults$,
  };
};

export default ServerResults;
