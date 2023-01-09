# This file exists to generate all of the Vector classes (Vector3f, Vector3d, etc).

template_vectorClass="/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/

// This class was auto-generated by \"generate_vector_classes.sh\".
// DO NOT edit this file directly! Instead, edit the template at the top of the generator script.

package co.casterlabs.commons.functional.vectors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * A Vector class with *size components: *fieldNames. This class is mutable.
 */
@NoArgsConstructor
@AllArgsConstructor
public class Vector*size*type_s {
    public *type *fieldNames;

    /**
     * @return An array with all *size components.
     */
    public *type[] toArray() {
        return new *type[] { *fieldNames };
    }

}"

dataTypes=("byte" "short" "int" "float" "long" "double")
allFieldNames=(_ "a" "b" "c" "d")
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
    # Turn ", a, b, ..." into "a, b, ...""
    fieldNames=${fieldNames:2}

    # Generate the template, replacing the `*size`, `*fields`, and `*fieldNames` placeholders.
    template="$template_vectorClass";
    template="${template//"*size"/$size}";
    template="${template//"*fieldNames"/$fieldNames}";

    for type in ${dataTypes[@]}; do
        type_s=${type:0:1}
    
        # Generate the class file, replacing the remaining placeholders.
        classFile="$template"
        classFile="${classFile//"*type_s"/$type_s}"
        classFile="${classFile//"*type"/$type}"

        fileName="Vector$size$type_s.java"

        echo "$classFile" >> "$baseDir/$fileName"
    done
done