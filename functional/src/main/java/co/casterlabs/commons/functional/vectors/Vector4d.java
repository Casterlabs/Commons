/* 
Copyright 2024 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/

package co.casterlabs.commons.functional.vectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

// This class was auto-generated by "generate_vector_classes.sh".
// DO NOT edit this file directly! Instead, edit the template at the top of the generator script.

/**
 * A Vector class with 4 components: x, y, z, w. This class is immutable.
 */
@ToString
@RequiredArgsConstructor
public class Vector4d {
    public final double x, y, z, w;

	public final int components = 4;

	/**
	 * Constructs a vector, using the provided array for the fields. 
	 * @apiNote the array must be at least 4 elements long.
	 */
    public Vector4d(@NonNull double[] arr) {
		this(arr[1], arr[2], arr[3], arr[4]);
	}
    
    /**
     * @return An array with all 4 components.
     */
    public double[] toArray() {
        return new double[] { x, y, z, w };
    }
    
    /* -------------------- */
    /* Math                 */
    /* -------------------- */
    
	/**
	 * Adds the given vector with this one, returning a new vector with the result.
	 */
    public Vector4d add(@NonNull Vector4d other) {
		double[] arr = this.toArray();
		double[] otherArr = other.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] += otherArr[idx];
		}
		
		return new Vector4d(arr);
	}

	/**
	 * Subtracts the given vector with this one, returning a new vector with the result.
	 */
    public Vector4d sub(@NonNull Vector4d other) {
		double[] arr = this.toArray();
		double[] otherArr = other.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] -= otherArr[idx];
		}
		
		return new Vector4d(arr);
	}

	/**
	 * Multiplies this vector with given number, returning a new vector with the result.
	 */
    public Vector4d mul(double by) {
		double[] arr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] *= by;
		}
		
		return new Vector4d(arr);
	}

	/**
	 * Divides this vector with given number, returning a new vector with the result.
	 */
    public Vector4d div(double by) {
		double[] arr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] /= by;
		}
		
		return new Vector4d(arr);
	}

	/**
	 * @implNote Not guaranteed to be strict (or accurate). This has the advantage 
	 *           of possibly being faster than software-based strict math.
	 */
    public double magnitude() {
		double mag = 0;
		
		double[] thisArr = this.toArray();
		for (int idx = 0; idx < this.components; idx++) {
			mag += thisArr[idx] * thisArr[idx];
		}
		
		return (double) Math.sqrt(mag);
	}

	/**
	 * @implNote Performs a strict sqrt to get the resulting magnitude.
	 */
    public double magnitude_strict() {
		double mag = 0;
		
		double[] thisArr = this.toArray();
		for (int idx = 0; idx < this.components; idx++) {
			mag += thisArr[idx] * thisArr[idx];
		}
		
		return (double) StrictMath.sqrt(mag);
	}

    /* -------------------- */
    /* Conversions          */
    /* -------------------- */
    
	/**
	 * Converts this vector to a byte vector.
	 */
	public Vector4b toByteVector() {
		byte[] arr = new byte[this.components];
		double[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (byte) thisArr[idx];
		}
		
		return new Vector4b(arr);
	}

	/**
	 * Converts this vector to a short vector.
	 */
	public Vector4s toShortVector() {
		short[] arr = new short[this.components];
		double[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (short) thisArr[idx];
		}
		
		return new Vector4s(arr);
	}

	/**
	 * Converts this vector to a int vector.
	 */
	public Vector4i toIntVector() {
		int[] arr = new int[this.components];
		double[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (int) thisArr[idx];
		}
		
		return new Vector4i(arr);
	}

	/**
	 * Converts this vector to a float vector.
	 */
	public Vector4f toFloatVector() {
		float[] arr = new float[this.components];
		double[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (float) thisArr[idx];
		}
		
		return new Vector4f(arr);
	}

	/**
	 * Converts this vector to a long vector.
	 */
	public Vector4l toLongVector() {
		long[] arr = new long[this.components];
		double[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (long) thisArr[idx];
		}
		
		return new Vector4l(arr);
	}

	/**
	 * Converts this vector to a double vector.
	 */
	public Vector4d toDoubleVector() {
		double[] arr = new double[this.components];
		double[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (double) thisArr[idx];
		}
		
		return new Vector4d(arr);
	}

}
