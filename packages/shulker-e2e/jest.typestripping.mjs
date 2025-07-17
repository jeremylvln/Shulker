import { stripTypeScriptTypes } from 'node:module';

export default {
  process(sourceText) {
    return { code: stripTypeScriptTypes(sourceText) };
  },
};
