inlets = 1 // a matrix, 1-d only please
outlets = 1 // a max message

var SIZE = 1024; // max array size

var workspace = new JitterMatrix();
// allocate only once
var out = Array(SIZE);
var null_arr = ["N"]

function jit_matrix(in_name) {
	workspace.frommatrix(in_name);
	var cell = new Array(1);
	var i=0;
	for (; i<SIZE; i++) {
		cell = workspace.getcell(i) || null_arr;
		out[i] = cell[0];
	}
	
	outlet(0, out);
}