/* 
Copyright 2022 Casterlabs

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package co.casterlabs.commons.platform;

public class Platform {

	/** The CPU Architecture of the host, e.g amd64 or aarch64. */
	public static final Arch arch = Arch.get();

	/** The family of the host's OS, e.g macOS or Windows NT */
	public static final OSFamily osFamily = OSFamily.get();

	/** The family distribution of the host's OS, e.g Unix or Windows */
	public static final OSDistribution osDistribution = OSDistribution.get(osFamily);

}