const fs = require('fs');

const origReadlink = fs.readlink;
fs.readlink = function(path, options, callback) {
  if (typeof options === 'function') {
    callback = options;
    options = {};
  }
  return origReadlink.call(fs, path, options, (err, linkString) => {
    if (err && err.code === 'EISDIR') {
      const newErr = new Error(err.message);
      newErr.code = 'EINVAL';
      return callback ? callback(newErr) : undefined;
    }
    return callback ? callback(err, linkString) : undefined;
  });
};

const origReadlinkSync = fs.readlinkSync;
fs.readlinkSync = function(path, options) {
  try {
    return origReadlinkSync.call(fs, path, options);
  } catch (err) {
    if (err && err.code === 'EISDIR') {
      const newErr = new Error(err.message);
      newErr.code = 'EINVAL';
      throw newErr;
    }
    throw err;
  }
};

if (fs.promises && fs.promises.readlink) {
  const origPromisesReadlink = fs.promises.readlink;
  fs.promises.readlink = async function(path, options) {
    try {
      return await origPromisesReadlink.call(fs.promises, path, options);
    } catch (err) {
      if (err && err.code === 'EISDIR') {
        const newErr = new Error(err.message);
        newErr.code = 'EINVAL';
        throw newErr;
      }
      throw err;
    }
  };
}
