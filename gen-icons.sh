#!/bin/bash
#
# Generate icons from the SVG. Ideally, this should be done in the ant task,
# but this really only happens rarely, whenever there is tweaking of the icon.
#
# Generated icons are checked in, so no need to run this script every time.
##

BASE_DRAWABLE=res/drawable

declare -A resolutions=( ["xhdpi"]="96" ["hdpi"]="72" ["mdpi"]="48")
for res in "${!resolutions[@]}"; do
    SIZE="${resolutions[$res]}"
    inkscape --export-png=${BASE_DRAWABLE}-${res}/app_icon.png \
	     --export-width=$SIZE --export-height=$SIZE precise-pitch.svg
done

