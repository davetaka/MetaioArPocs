arel.sceneReady(function()
{
	console.log("sceneReady");

	//set a listener to tracking to get information about when the image is tracked
	arel.Events.setListener(arel.Scene, function(type, param){trackingHandler(type, param);});
                
	//get the metaio man model reference
	var metaioman = arel.Scene.getObject("1");
                
});

function trackingHandler(type, param)
{
	//check if there is tracking information available
	if(param[0] !== undefined)
	{
		//if the pattern is found, hide the information to hold your phone over the pattern
		if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_TRACKING)
		{
            arel.Scene.getObject("1").setCoordinateSystemID( param[0].getCoordinateSystemID() );
		}
		//if the pattern is lost tracking, show the information to hold your phone over the pattern
		else if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_NOTTRACKING)
		{

		}
	}
};

function clickHandler()
{
    var idClicked = $("#radio :radio:checked").attr('id');
    if (idClicked == 'radio1')
    {
        arel.Scene.setTrackingConfiguration("TrackingData_Marker.xml");
        arel.Scene.getObject("1").setScale(new arel.Vector3D(2.0,2.0,2.0));
    }
    if (idClicked == 'radio2')
    {
        arel.Scene.setTrackingConfiguration("TrackingData_PictureMarker.xml");
        arel.Scene.getObject("1").setScale(new arel.Vector3D(8.0,8.0,8.0));
        
    }
    if (idClicked == 'radio3')
    {
        arel.Scene.setTrackingConfiguration("TrackingData_MarkerlessFast.xml");
        arel.Scene.getObject("1").setScale(new arel.Vector3D(4.0,4.0,4.0));
    }
};