# This file exists to generate all of the Vector classes (Vector3f, Vector3d, etc).

template_vectorClass="/* 
Copyright 2024 Casterlabs

Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/

package co.casterlabs.commons.functional.vectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

// This class was auto-generated by \"generate_vector_classes.sh\".
// DO NOT edit this file directly! Instead, edit the template at the top of the generator script.

/**
 * A Vector class with *size components: *fieldNames. This class is immutable.
 */
@ToString
@RequiredArgsConstructor
public class Vector*size*type_s {
    public final *type *fieldNames;

	public final int components = *size;

	/**
	 * Constructs a vector, using the provided array for the fields. 
	 * @apiNote the array must be at least *size elements long.
	 */
    public Vector*size*type_s(@NonNull *type[] arr) {
		this(*arrSpread);
	}
    
    /**
     * @return An array with all *size components.
     */
    public *type[] toArray() {
        return new *type[] { *fieldNames };
    }
    
    /* -------------------- */
    /* Math                 */
    /* -------------------- */
    
	/**
	 * Adds the given vector with this one, returning a new vector with the result.
	 */
    public Vector*size*type_s add(@NonNull Vector*size*type_s other) {
		*type[] arr = this.toArray();
		*type[] otherArr = other.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] += otherArr[idx];
		}
		
		return new Vector*size*type_s(arr);
	}

	/**
	 * Subtracts the given vector with this one, returning a new vector with the result.
	 */
    public Vector*size*type_s sub(@NonNull Vector*size*type_s other) {
		*type[] arr = this.toArray();
		*type[] otherArr = other.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] -= otherArr[idx];
		}
		
		return new Vector*size*type_s(arr);
	}

	/**
	 * Multiplies this vector with given number, returning a new vector with the result.
	 */
    public Vector*size*type_s mul(*type by) {
		*type[] arr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] *= by;
		}
		
		return new Vector*size*type_s(arr);
	}

	/**
	 * Divides this vector with given number, returning a new vector with the result.
	 */
    public Vector*size*type_s div(*type by) {
		*type[] arr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] /= by;
		}
		
		return new Vector*size*type_s(arr);
	}

	/**
	 * @implNote Not guaranteed to be strict (or accurate). This has the advantage 
	 *           of possibly being faster than software-based strict math.
	 */
    public *type magnitude() {
		*mathType mag = 0;
		
		*type[] thisArr = this.toArray();
		for (int idx = 0; idx < this.components; idx++) {
			mag += thisArr[idx] * thisArr[idx];
		}
		
		return (*type) Math.sqrt(mag);
	}

	/**
	 * @implNote Performs a strict sqrt to get the resulting magnitude.
	 */
    public *type magnitude_strict() {
		*mathType mag = 0;
		
		*type[] thisArr = this.toArray();
		for (int idx = 0; idx < this.components; idx++) {
			mag += thisArr[idx] * thisArr[idx];
		}
		
		return (*type) StrictMath.sqrt(mag);
	}

    /* -------------------- */
    /* Conversions          */
    /* -------------------- */
    
	/**
	 * Converts this vector to a byte vector.
	 */
	public Vector*sizeb toByteVector() {
		byte[] arr = new byte[this.components];
		*type[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (byte) thisArr[idx];
		}
		
		return new Vector*sizeb(arr);
	}

	/**
	 * Converts this vector to a short vector.
	 */
	public Vector*sizes toShortVector() {
		short[] arr = new short[this.components];
		*type[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (short) thisArr[idx];
		}
		
		return new Vector*sizes(arr);
	}

	/**
	 * Converts this vector to a int vector.
	 */
	public Vector*sizei toIntVector() {
		int[] arr = new int[this.components];
		*type[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (int) thisArr[idx];
		}
		
		return new Vector*sizei(arr);
	}

	/**
	 * Converts this vector to a float vector.
	 */
	public Vector*sizef toFloatVector() {
		float[] arr = new float[this.components];
		*type[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (float) thisArr[idx];
		}
		
		return new Vector*sizef(arr);
	}

	/**
	 * Converts this vector to a long vector.
	 */
	public Vector*sizel toLongVector() {
		long[] arr = new long[this.components];
		*type[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (long) thisArr[idx];
		}
		
		return new Vector*sizel(arr);
	}

	/**
	 * Converts this vector to a double vector.
	 */
	public Vector*sized toDoubleVector() {
		double[] arr = new double[this.components];
		*type[] thisArr = this.toArray();
		
		for (int idx = 0; idx < this.components; idx++) {
			arr[idx] = (double) thisArr[idx];
		}
		
		return new Vector*sized(arr);
	}

}"

dataTypes=("byte" "short" "int" "float" "long" "double")
allFieldNames=(_ "x" "y" "z" "w" "v" "u" "t" "s" "r" "q" "p")
sizes=(2 3 4)

baseDir="src/main/java/co/casterlabs/commons/functional/vectors"

# Empty out the directory.
rm -rf $baseDir
mkdir $baseDir

for size in ${sizes[@]}; do

    # Build the fieldNames string. When done, this looks like:
    # , a, b, ...
    fieldNames=""
    for idx in $(seq $size); do
        fieldName=${allFieldNames[$idx]}
        fieldNames="$fieldNames, $fieldName"
    done
    # Turn ", a, b, ..." into "a, b, ..."
    fieldNames=${fieldNames:2}

    # Build the arrSpread string. When done, this looks like:
    # , arr[0], arr[1] ...
    arrSpread=""
    for idx in $(seq $size); do
        arrSpread="$arrSpread, arr[$idx]"
    done
    # Turn ", arr[0], arr[1] ..." into "arr[0], arr[1] ..."
    arrSpread=${arrSpread:2}

    # Generate the template, replacing the `*size`, `*fields`, and `*fieldNames` placeholders.
    template="$template_vectorClass";
    template="${template//"*size"/$size}";
    template="${template//"*fieldNames"/$fieldNames}";
    template="${template//"*arrSpread"/$arrSpread}";

    for type in ${dataTypes[@]}; do
        type_s=${type:0:1}
        
        floatTypes=("double" "float")
        if [[ ${floatTypes[@]} =~ $type ]]
		then
			mathType="double"
		else
			mathType="long"
		fi
    
        # Generate the class file, replacing the remaining placeholders.
        classFile="$template"
        classFile="${classFile//"*type_s"/$type_s}"
        classFile="${classFile//"*type"/$type}"
        classFile="${classFile//"*mathType"/$mathType}"

        fileName="Vector$size$type_s.java"

        echo "$classFile" >> "$baseDir/$fileName"
    done
done