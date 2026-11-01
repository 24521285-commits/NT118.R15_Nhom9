/**
 * Mọi response đều có cùng dạng để Android (Retrofit) dễ map vào BaseResponse<T>:
 *   { "success": true,  "message": "...", "data": {...}, "meta": {...} }   (meta: không bắt buộc)
 *   { "success": false, "message": "...", "code": "...", "errors": [...] }
 */
function ok(res, data = null, message = 'OK', status = 200, meta = undefined) {
  return res.status(status).json({ success: true, message, data, meta });
}

function created(res, data = null, message = 'Tạo thành công') {
  return ok(res, data, message, 201);
}

module.exports = { ok, created };
