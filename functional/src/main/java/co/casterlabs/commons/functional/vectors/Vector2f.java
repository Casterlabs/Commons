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
 * A Vector class with 2 components: x, y. This class is immutable.
 */
@ToString
@RequiredArgsConstructor
public class Vector2f {
    public final float x, y;

	public final int components = 2;

	/**
	 * Constructs a vector, using the provided array for the fields. 
	 * @apiNote the array must be at least 2 elements long.
	 */
    public Vector2f(@NonNull float[] arr) {
		this(arr[1], arr[2]);
	}
    
    /**
     * @return An array with all 2 components.
     */
    public float[] toArray() {
        return new float[] { x, y };
    }
    
    /* -------------------- */
    /* Math                 */
    /* -------------------- */
    
	/**
	 * Adds the given vector with this one, returning a new vector with the result.
	 */
    public Vector2f add(@NonNull Vector2f other) {
		float[] arr = this.toArray();
		float[] otherArr = other.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] += otherArr[idx];
		}
		
		return new Vector2f(arr);
	}

	/**
	 * Subtracts the given vector with this one, returning a new vector with the result.
	 */
    public Vector2f sub(@NonNull Vector2f other) {
		float[] arr = this.toArray();
		float[] otherArr = other.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] -= otherArr[idx];
		}
		
		return new Vector2f(arr);
	}

	/**
	 * Multiplies this vector with given number, returning a new vector with the result.
	 */
    public Vector2f mul(float by) {
		float[] arr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] *= by;
		}
		
		return new Vector2f(arr);
	}

	/**
	 * Divides this vector with given number, returning a new vector with the result.
	 */
    public Vector2f div(float by) {
		float[] arr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] /= by;
		}
		
		return new Vector2f(arr);
	}

	/**
	 * @implNote Not guaranteed to be strict (or accurate). This has the advantage 
	 *           of possibly being faster than software-based strict math.
	 */
    public float magnitude() {
		double mag = 0;
		
		float[] thisArr = this.toArray();
		for (int idx = 0; idx < this.components; idx++) {
			mag += thisArr[idx] * thisArr[idx];
		}
		
		return (float) Math.sqrt(mag);
	}

	/**
	 * @implNote Performs a strict sqrt to get the resulting magnitude.
	 */
    public float magnitude_strict() {
		double mag = 0;
		
		float[] thisArr = this.toArray();
		for (int idx = 0; idx < this.components; idx++) {
			mag += thisArr[idx] * thisArr[idx];
		}
		
		return (float) StrictMath.sqrt(mag);
	}

    /* -------------------- */
    /* Conversions          */
    /* -------------------- */
    
	/**
	 * Converts this vector to a byte vector.
	 */
	public Vector2b toByteVector() {
		byte[] arr = new byte[this.components];
		float[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (byte) thisArr[idx];
		}
		
		return new Vector2b(arr);
	}

	/**
	 * Converts this vector to a short vector.
	 */
	public Vector2s toShortVector() {
		short[] arr = new short[this.components];
		float[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (short) thisArr[idx];
		}
		
		return new Vector2s(arr);
	}

	/**
	 * Converts this vector to a int vector.
	 */
	public Vector2i toIntVector() {
		int[] arr = new int[this.components];
		float[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (int) thisArr[idx];
		}
		
		return new Vector2i(arr);
	}

	/**
	 * Converts this vector to a float vector.
	 */
	public Vector2f toFloatVector() {
		float[] arr = new float[this.components];
		float[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (float) thisArr[idx];
		}
		
		return new Vector2f(arr);
	}

	/**
	 * Converts this vector to a long vector.
	 */
	public Vector2l toLongVector() {
		long[] arr = new long[this.components];
		float[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (long) thisArr[idx];
		}
		
		return new Vector2l(arr);
	}

	/**
	 * Converts this vector to a double vector.
	 */
	public Vector2d toDoubleVector() {
		double[] arr = new double[this.components];
		float[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (double) thisArr[idx];
		}
		
		return new Vector2d(arr);
	}

}
