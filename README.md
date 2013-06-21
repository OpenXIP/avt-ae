Welcome to the AVT Algorithm Execution&trade; Project!
======================================================

The Algorithm Validation Toolkit&trade; (AVT&trade;) Open Source project is a set
of tools that facilitate the testing and statistical comparison of image processing
algorithms.  It is built upon the
[eXtensible Imaging Platform&trade; (XIP&trade;)](http://www.OpenXIP.org) development
framework, and can create and consume objects based
on [AIM (Annotation and Image Markup] (https://github.com/NCIP/annotation-and-image-markup)
models.  

The Algorithm Execution&trade; component of AVT&trade; (AD&trade;) is an XIP-based 
[DICOM Hosted Application](http://medical.nema.org/Dicom/2011/11_19pu.pdf)
that can iterate through a set of studies provided through a 
[DICOM Hosting System](http://medical.nema.org/Dicom/2011/11_19pu.pdf),
such as the [XIP Host&trade;](https://github.com/OpenXIP/xip-host).
AE&trade; is implemented in Java with Swing, and utilizes scene graphs
created with the [XIP Libraries&trade;](https://github.com/OpenXIP/xip-libraries).

The ultimate goal of the project is to provide a mechanism to repeatedly run an 
algorithm against a set of studies, The algorithm is specified via a scene graph with 
pipeline built using the [XIP Libraries&trade;](https://github.com/OpenXIP/xip-libraries).

AVT&trade;, including AVT AD Install&trade; is distributed under the
[Apache 2.0 License](http://opensource.org/licenses/Apache-2.0).
Please see the NOTICE and LICENSE files for details.

You will find more details about AVT&trade; and XIP&trade; in the following links:

*  [Home Page] (http://www.OpenXIP.org)
*  [Forum/Mailing List] (https://groups.google.com/forum/?fromgroups#!forum/openxip)
*  [Issue tracker] (https://plans.imphub.org/browse/XIP)
*  [Documentation] (https://docs.imphub.org/display/XIP)
*  [Git code repository] (https://github.com/OpenXIP/xip-host)
