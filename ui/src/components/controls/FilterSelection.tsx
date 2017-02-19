import xs, {Stream, MemoryStream} from 'xstream';
import {VNode, CycleDOMEvent} from '@cycle/dom';
import {DOMSource} from '@cycle/dom/xstream-typings';
const {html} = require('snabbdom-jsx');
import Config from '../../services/Config';

type ISources = {
    DOM: DOMSource,
};

export type IFilter = 'GREYSCALE' | 'HALFTONE' | 'CHROME';

export type IFilterSelection = IFilter | null;

export type ISinks = {
    DOM: Stream<JSX.Element>,
    FILTER: Stream<IFilterSelection>
}


const FrequencyControl = (sources: ISources): ISinks => {

    const filterSelection$ = sources
        .DOM
        .select('#filterSelection')
        .events('change')
        .map(event => {
            const selection = (event.target as HTMLSelectElement).value
            return selection === 'UNSET' ? null : selection as IFilterSelection
        })
        .startWith(null)

    const filters: IFilter[] = [
        'GREYSCALE',
        'HALFTONE',
        'CHROME'
    ];

    const filterSelectionControl$ = filterSelection$.map(selected =>
        <div className='col col-xs-12 mb-1'>
            <h5>Filter Selection</h5>
            <select name='filterSelection' id='filterSelection'>
                <option
                    value={ undefined }
                    selected={selected == null ? true : undefined}
                    style={{ textTransform: 'capitalize' }}>
                    UNSET
                </option>
                <optgroup label='Available:'>
                    {
                        filters.map(name =>
                            <option
                                value={name}
                                selected={name === selected ? true : undefined}
                                style={{ textTransform: 'capitalize' }}
                            >
                                {name}
                            </option>
                        )
                    }
                </optgroup>
            </select>
        </div>
    );

    return {
        DOM: filterSelectionControl$,
        FILTER: filterSelection$
    }
};

export default FrequencyControl;
