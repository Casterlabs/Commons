/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/

// This class was auto-generated by "generate_vector_classes.sh".
// DO NOT edit this file directly! Instead, edit the template at the top of the generator script.

package co.casterlabs.commons.functional.vectors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * A Vector class with 3 components: a, b, c. This class is mutable.
 */
@NoArgsConstructor
@AllArgsConstructor
public class Vector3b {
    public byte a, b, c;

    /**
     * @return An array with all 3 components.
     */
    public byte[] toArray() {
        return new byte[] { a, b, c };
    }

}
