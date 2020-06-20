import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';
import reducer from './reducers/index';

const configureStore = (persistedState) => {
 return createStore(
   reducer,
   persistedState,
   applyMiddleware(thunk)
 );
}

export default configureStore;