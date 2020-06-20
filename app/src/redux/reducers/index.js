import { combineReducers } from 'redux';
import userReducer from './userReducer';
import groupReducer from './groupReducer';
import { appReducer } from './appReducer';

export default combineReducers({
    app: appReducer,
    user: userReducer,
    groups: groupReducer
})