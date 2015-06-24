/*
 * Copyright 2015 Henner Zeller <h.zeller@acm.org>
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
 */
package net.zllr.precisepitch.model;

// Some convenient constants.
public class Note {
    public static final int C   = 3;
    public static final int C_s = 4;
    public static final int D_b = 4;
    public static final int D   = 5;
    public static final int D_s = 6;
    public static final int E_b = 6;
    public static final int E   = 7;
    public static final int F   = 8;
    public static final int F_s = 9;
    public static final int G_b = 9;
    public static final int G   = 10;
    public static final int G_s = 11;
    public static final int A_b = 11;
    public static final int A   = 12;
    public static final int A_s = 13;
    public static final int B_b = 13;
    public static final int B   = 14;

    public static final int c   = C   + 12;
    public static final int c_s = C_s + 12;
    public static final int d_b = D_b + 12;
    public static final int d   = D   + 12;
    public static final int d_s = D_s + 12;
    public static final int e_b = E_b + 12;
    public static final int e   = E   + 12;
    public static final int f   = F   + 12;
    public static final int f_s = F_s + 12;
    public static final int g_b = G_b + 12;
    public static final int g   = G   + 12;
    public static final int g_s = G_s + 12;
    public static final int a_b = A_b + 12;
    public static final int a   = A   + 12;
    public static final int a_s = A_s + 12;
    public static final int b_b = B_b + 12;
    public static final int b   = B   + 12;

    // Since we can't use a proper prime sign, let's use $
    public static final int c$   = c   + 12;
    public static final int c$_s = c_s + 12;
    public static final int d$_b = d_b + 12;
    public static final int d$   = d   + 12;
    public static final int d$_s = d_s + 12;
    public static final int e$_b = e_b + 12;
    public static final int e$   = e   + 12;
    public static final int f$   = f   + 12;
    public static final int f$_s = f_s + 12;
    public static final int g$_b = g_b + 12;
    public static final int g$   = g   + 12;
    public static final int g$_s = g_s + 12;
    public static final int a$_b = a_b + 12;
    public static final int a$   = a   + 12;
    public static final int a$_s = a_s + 12;
    public static final int b$_b = b_b + 12;
    public static final int b$   = b   + 12;
}
