Drop courier photos here as <courierId>.jpg (or .jpeg / .png), e.g. 1.jpg, 2.jpg.
The CourierPhotos util looks for /images/couriers/<id>.{jpg,jpeg,png} on the
classpath and renders a colored initials avatar as a fallback when no file
matches. Images are loaded once per courier and cached.

Recommended: square JPGs, 80x80 px or larger. They are clipped to a circle
and scaled to 40x40 in the couriers table.
