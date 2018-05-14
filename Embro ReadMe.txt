Embro - A free and open-source application intended to generate patterns and make certain disciplines of
Embroidery and tapestry creation easier to keep track of. Embro is provided as-is with no warranty.

To use the program, place Embro.jar into a folder of your choosing and run it.


The first screen is for loading Pictures or pre-existing Projects. If there are valid types of image files in
that folder, it will populate a list with them on the right side, and on the left side will be Projects that
you've already created.

-	To load an image stored elsewhere in your computer, copy the full path
	(e.g. C:\Users\Me\Pictures\Flower.png) into the text field at the top of the screen, then click the
	"Load" button in the top right corner.

-	To select a local image, click it then click the "Load" button.

-	To continue a project that you've already started, click it then click the "Load" button.


The second screen is for converting the raw Picture into something that looks more like a Tapestry.
You will need to specify how many units you want to span across the piece, and how many individual colours
you want to use to make it. You may press enter or the button to show you the result at any time, it will
update the result if you change those numbers.

-	Enter how many units you want there to be across the final artwork into the first text field.

-	Enter how many colours you want to use in the second text field.

-	To navigate around an image, press the arrow keys to move around, press "Page Up" and "Page Down"
	to change the zoom, and press "Home" to go back to the default view.

Once there is a valid number in both text fields, it will allow you to click the "Continue" button.
At this stage you may also manually move colours around by Right-clicking to pick up a colour and
Left-clicking to place it.

Note that the artwork must be fewer units across than the number of pixels across the source picture, and
that the colour palette compressor may merge together similar colours that you intended to be separate,
even if the total number of colours in the source image is the same as the number specified. To force the
program to use the same colour palette, or the same width, as your source image, enter the keyword "keep"
into the respective text fields.


Now you can start working! The program has created a Project and given it the name of your source image.

There are three main information areas:

-	The Palette, which shows how many in total of each colour are in each line, and ends with a count
	of how many individual colours are present in that line.

-	The Working area, which shows the artwork, and can display your own highlighting and progress.

-	The Streak tallies, which count how many units of a colour need to be added until it changes.

The same navigation controls apply as in the previous screen, and also you may press "Tab" to move between
aligned with the palette, the working area, and the streaks.

To highlight a unit in the working area, Left-click toggles a forward-slash, and Right-click toggles a
backslash.

To mark your current progress through the artwork, press "Space Bar" to advance it one unit, and "Enter" to
complete the current line. To undo accidental advances, hold "Ctrl" and press "Z". These and the
highlighting are saved automatically and continuously.

The program will create a Sub-folder in it's Folder called Projects, and in here it will save all created
Projects as a .PNG image, and a .txt file that keeps track of your progress and highlighting.

Note that if you create another Project using an image with the same file name, even if it is stored
somewhere else, it will over-write the previously created Project.

To load a different Project or picture, exit the program and run it again.


This software is not intended to improve the performance of cyclists in cold weather.



This program uses an implementation of QUANTIZE, which is distributed with the following license:
Obtained from http://www.java2s.com/Code/Java/2D-Graphics-GUI/Anefficientcolorquantizationalgorithm.htm


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                                                                             %
%                                                                             %
%                                                                             %
%           QQQ   U   U   AAA   N   N  TTTTT  IIIII   ZZZZZ  EEEEE            %
%          Q   Q  U   U  A   A  NN  N    T      I        ZZ  E                %
%          Q   Q  U   U  AAAAA  N N N    T      I      ZZZ   EEEEE            %
%          Q  QQ  U   U  A   A  N  NN    T      I     ZZ     E                %
%           QQQQ   UUU   A   A  N   N    T    IIIII   ZZZZZ  EEEEE            %
%                                                                             %
%                                                                             %
%              Reduce the Number of Unique Colors in an Image                 %
%                                                                             %
%                                                                             %
%                           Software Design                                   %
%                             John Cristy                                     %
%                              July 1992                                      %
%                                                                             %
%                                                                             %
%  Copyright 1998 E. I. du Pont de Nemours and Company                        %
%                                                                             %
%  Permission is hereby granted, free of charge, to any person obtaining a    %
%  copy of this software and associated documentation files ("ImageMagick"),  %
%  to deal in ImageMagick without restriction, including without limitation   %
%  the rights to use, copy, modify, merge, publish, distribute, sublicense,   %
%  and/or sell copies of ImageMagick, and to permit persons to whom the       %
%  ImageMagick is furnished to do so, subject to the following conditions:    %
%                                                                             %
%  The above copyright notice and this permission notice shall be included in %
%  all copies or substantial portions of ImageMagick.                         %
%                                                                             %
%  The software is provided "as is", without warranty of any kind, express or %
%  implied, including but not limited to the warranties of merchantability,   %
%  fitness for a particular purpose and noninfringement.  In no event shall   %
%  E. I. du Pont de Nemours and Company be liable for any claim, damages or   %
%  other liability, whether in an action of contract, tort or otherwise,      %
%  arising from, out of or in connection with ImageMagick or the use or other %
%  dealings in ImageMagick.                                                   %
%                                                                             %
%  Except as contained in this notice, the name of the E. I. du Pont de       %
%  Nemours and Company shall not be used in advertising or otherwise to       %
%  promote the sale, use or other dealings in ImageMagick without prior       %
%  written authorization from the E. I. du Pont de Nemours and Company.       %
%                                                                             %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%