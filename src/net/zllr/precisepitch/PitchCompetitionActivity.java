/*
 * Copyright 2013 Henner Zeller <h.zeller@acm.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Rough sequence:
 *   - choose 1 or 2 players
 *   - display practice activity
 *         o choose chant of doom. Right now: pre-select
 *           Game data: Staff model with DisplayNotes, #players game result data.
 *         o have two colored buttons. Let first player choose.
 *         o Game goes on with practice activity, accepting +/- 45 cent, displays
 *           time. Records results for player #1
 *         o When done, the other button shows up. Pressing that goes to the
 *           practice activity, records results for player #2
 *         o When all results are collected, gets to the result display page
 *             - display raw scores at first.
 *             - next: toggle between car-race and histogram annotated notes.
 */
package net.zllr.precisepitch;

import android.app.Activity;

public class PitchCompetitionActivity extends Activity {
}
