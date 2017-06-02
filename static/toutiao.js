function crc32(t) {
	var n = function() {
	    for (var t = 0, e = new Array(256), n = 0; 256 != n; ++n) {
		t = n;
		for ( var j = 0; j < 8; j++ ) {
		    t = 0x01 & t ? -306674912 ^ t >>> 1 : t >>> 1;
		}

		e[n] = t;
	    }

	    return e;
	    /*console.log("undefined" != typeof Int32Array);
	    return "undefined" != typeof Int32Array ? new Int32Array(e) : e*/
	} ();

	var new_n = function(t) {
	    for (var e, r = -1,
		         i = 0,
		         a = t.length; i < a;) {
		e = t.charCodeAt(i++);

		var _n = n[255 & (r ^ e)];
		r = r >>> 8 ^ _n;
	    }

	    return r ^ -1
	};

	var base = "/video/urls/v/1/toutiao/mp4/";
	var rand = Math.random();

	var r = rand.toString(10).substring(2);

	r = base+t+"?r=" + r;

	var _n = new_n(r);

	var i = _n >>> 0;

	return "http://ib.365yg.com" + r + "&s=" + i;
}
