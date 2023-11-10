/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.functional.tuples;

/**
 * A variable-length tuple without any types. Use this if you need a dirty way
 * of extending the existing line-up of Tuples.
 * 
 * @apiNote If you frequently need an additional Tuple, open a issue on
 *          github.com/Casterlabs/Commons and we'll add it for you :^)
 */
public class VariableTuple extends Tuple {

    /**
     * @param args The arguments to add, any of which can be null.
     */
    public VariableTuple(Object... args) {
        super(args.clone());
    }

}
