var timer;
var timer_is_on=0;

arel.sceneReady(function()
{
    console.log("sceneReady");           
    
    //set a listener to tracking to get information about when the image is tracked
    arel.Events.setListener(arel.Scene, function(type, param){trackingHandler(type, param);});
            
});

function trackingHandler(type, param)
{

	//check if there is tracking information available
	if(param[0] !== undefined)
	{
		//if the pattern is found, hide the information to hold your phone over the pattern
		if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_TRACKING)
		{
            if (!timer_is_on)
            {
                timer_is_on=1;
                arel.Scene.getTrackingValues(function(tv){receiveCurrentTrackingValues(tv);});
            }
            //timer = setInterval(function(){drawFrame();},1000);
		}
		//if the pattern is lost tracking, show the information to hold your phone over the pattern
		else if(type && type == arel.Events.Scene.ONTRACKING && param[0].getState() == arel.Tracking.STATE_NOTTRACKING)
		{
            if (timer_is_on)
            {
                clearTimeout(timer);
                timer_is_on=0;
            }
		}
	}
};


function clickHandler()
{
    if (timer_is_on)
    {
        clearTimeout(timer);
        timer_is_on=0;
    }
    arel.Scene.getObject("tiger").setScale(new arel.Vector3D(8.0,8.0,8.0)); 
    var idClicked = $("#radio :radio:checked").attr('id');
    if (idClicked == 'radio1')
    {
        var modelRotation = new arel.Rotation();
        modelRotation.setFromEulerAngleDegrees(new arel.Vector3D(0.0,0.0,180.0));
        arel.Scene.getObject("tiger").setRotation(modelRotation);
        arel.Scene.startInstantTracking(arel.Tracking.INSTANT2D);
    }
    if (idClicked == 'radio2')
    {
        var modelRotation = new arel.Rotation();
        modelRotation.setFromEulerAngleDegrees(new arel.Vector3D(0.0,0.0,-180.0));
        arel.Scene.getObject("tiger").setRotation(modelRotation);
        arel.Scene.startInstantTracking(arel.Tracking.INSTANT2DG);
        
    }
    if (idClicked == 'radio3')
    {
        arel.Scene.startInstantTracking(arel.Tracking.INSTANT3D);

    }
};

function receiveCurrentTrackingValues(tv)
{
    if(tv[0] !== undefined)
    {
        var quality = tv[0].getQuality();
        if (parseFloat(quality) > 0.0)
        {
        
            var poseTranslation = tv[0].getTranslation();
            var threshold = 250.0;
    
            var distanceToTarget = Math.sqrt(poseTranslation.getX() * poseTranslation.getX() + poseTranslation.getY() * poseTranslation.getY() +poseTranslation.getZ() * poseTranslation.getZ());
     
            if(parseFloat(distanceToTarget) < threshold)
            {
                arel.Media.startSound("meow.mp3");
                arel.Scene.getObject("tiger").startAnimation("meow");
            }
        
        }
    }
    timer = setTimeout(function(){arel.Scene.getTrackingValues(function(tv){receiveCurrentTrackingValues(tv);});}, 1000);
};